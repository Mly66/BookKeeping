<template>
  <div class="ai-chat-container">
    <div class="chat-header">
      <h2>🤖 AI智能记账助手</h2>
      <p>用自然语言管理您的账单，支持增删改查操作</p>
      <div class="header-actions">
        <button @click="clearHistory" class="clear-btn">🗑️ 清除历史</button>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(message, index) in messages" :key="index" 
           :class="['message', message.type]">
        <div class="message-content">
          <div class="message-text">{{ message.text }}</div>
          <div v-if="message.data" class="message-data">
            <!-- 账单列表显示 -->
            <div v-if="Array.isArray(message.data) && message.data.length > 0 && message.data[0].amount !== undefined" class="bill-list">
              <div v-for="(bill, billIndex) in message.data" :key="billIndex" class="bill-item">
                <div class="bill-header">
                  <span class="bill-id">#{{ bill.id }}</span>
                  <span :class="['bill-type', bill.type === 'income' ? 'income' : 'expense']">
                    {{ bill.type === 'income' ? '收入' : '支出' }}
                  </span>
                  <span class="bill-amount" :class="bill.type === 'income' ? 'income' : 'expense'">
                    {{ bill.type === 'income' ? '+' : '-' }}￥{{ formatAmount(bill.amount) }}
                  </span>
                </div>
                <div class="bill-details">
                  <span class="bill-category">{{ bill.categoryName }}</span>
                  <span class="bill-time">{{ formatTime(bill.billTime) }}</span>
                </div>
                <div v-if="bill.remarks" class="bill-remarks">
                  {{ bill.remarks }}
                </div>
              </div>
            </div>
            <!-- 单个账单显示（创建账单时） -->
            <div v-else-if="message.data && !Array.isArray(message.data) && message.data.amount !== undefined" class="single-bill">
              <div class="bill-item created-bill">
                <div class="bill-header">
                  <span class="bill-id">#{{ message.data.id }}</span>
                  <span :class="['bill-type', message.data.type === 'income' ? 'income' : 'expense']">
                    {{ message.data.type === 'income' ? '收入' : '支出' }}
                  </span>
                  <span class="bill-amount" :class="message.data.type === 'income' ? 'income' : 'expense'">
                    {{ message.data.type === 'income' ? '+' : '-' }}￥{{ formatAmount(message.data.amount) }}
                  </span>
                </div>
                <div class="bill-details">
                  <span class="bill-category">{{ message.data.categoryName }}</span>
                  <span class="bill-time">{{ formatTime(message.data.billTime) }}</span>
                </div>
                <div v-if="message.data.remarks" class="bill-remarks">
                  {{ message.data.remarks }}
                </div>
              </div>
            </div>
            <!-- 单个分类显示 -->
            <div v-else-if="message.data && !Array.isArray(message.data) && message.data.name" class="single-category">
              <div class="category-item">
                <span class="category-name">{{ message.data.name }}</span>
                <span :class="['category-type', message.data.type === 'income' ? 'income' : 'expense']">
                  {{ message.data.type === 'income' ? '收入' : '支出' }}
                </span>
              </div>
            </div>
            <!-- 分类列表显示 -->
            <div v-else-if="Array.isArray(message.data) && message.data.length > 0 && message.data[0].name" class="category-list">
              <div v-for="(category, catIndex) in message.data" :key="catIndex" class="category-item">
                <span class="category-name">{{ category.name }}</span>
                <span :class="['category-type', category.type === 'income' ? 'income' : 'expense']">
                  {{ category.type === 'income' ? '收入' : '支出' }}
                </span>
              </div>
            </div>
            <!-- 其他数据格式 -->
            <div v-else class="raw-data">
              <pre>{{ JSON.stringify(message.data, null, 2) }}</pre>
            </div>
          </div>
        </div>
        <div class="message-time">{{ formatTime(message.time) }}</div>
      </div>
    </div>

    <div class="chat-input-container">
      <div class="input-wrapper">
        <input 
          v-model="inputMessage" 
          @keyup.enter="sendMessage"
          placeholder="例如：今天花了100元买午餐，收入5000元工资，查看所有账单..."
          class="chat-input"
          :disabled="isLoading"
        />
        <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()" class="send-button">
          <span v-if="isLoading">发送中...</span>
          <span v-else>发送</span>
        </button>
      </div>
    </div>

    <div class="quick-actions">
      <h4>快速操作示例：</h4>
      <div class="action-buttons">
        <button @click="quickAction('今天花了100元买午餐')" class="action-btn">记录支出</button>
        <button @click="quickAction('收入5000元工资')" class="action-btn">记录收入</button>
        <button @click="quickAction('查看所有账单')" class="action-btn">查看账单</button>
        <button @click="quickAction('查看账单统计')" class="action-btn">查看统计</button>
        <button @click="quickAction('查看我的分类')" class="action-btn">查看分类</button>
        <button @click="quickAction('帮助')" class="action-btn">获取帮助</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import apiClient from '@/api'

export default {
  name: 'AiChatView',
  setup() {
    const authStore = useAuthStore()
    const messages = ref([])
    const inputMessage = ref('')
    const isLoading = ref(false)
    const messagesContainer = ref(null)

    const addMessage = (text, type = 'user', data = null) => {
      messages.value.push({
        text,
        type,
        data,
        time: new Date()
      })
    }

    const scrollToBottom = async () => {
      await nextTick()
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    const sendMessage = async () => {
      if (!inputMessage.value.trim() || isLoading.value) return

      const message = inputMessage.value.trim()
      addMessage(message, 'user')
      inputMessage.value = ''
      isLoading.value = true

      try {
        const userId = authStore.user?.id
        if (!userId) {
          addMessage('请先登录以使用AI记账功能', 'ai')
          return
        }

        const response = await apiClient.get(`/ai/chat/simple?message=${encodeURIComponent(message)}&userId=${userId}`)
        const result = response.data
        
        if (typeof result === 'object' && result.message) {
          addMessage(result.message, 'ai', result.data)
        } else {
          addMessage(result, 'ai')
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        addMessage('抱歉，发送消息时出现错误，请稍后重试', 'ai')
      } finally {
        isLoading.value = false
        await scrollToBottom()
      }
    }

    const quickAction = (action) => {
      inputMessage.value = action
      sendMessage()
    }

    const formatTime = (time) => {
      if (typeof time === 'string') {
        // 处理账单时间格式
        const date = new Date(time)
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      }
      // 处理消息时间格式
      return new Date(time).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const formatAmount = (amount) => {
      if (amount == null || amount === undefined) {
        return '0.00'
      }
      return Number(amount).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }

    const clearHistory = () => {
      messages.value = []
      scrollToBottom()
    }

    onMounted(() => {
      addMessage('您好！我是您的智能记账助手 🤖\n\n我可以帮您：\n• 记录收入和支出\n• 查询账单信息\n• 更新账单内容\n• 删除不需要的账单\n• 查看统计信息\n• 管理账单分类\n\n请告诉我您需要什么帮助！', 'ai')
    })

    return {
      messages,
      inputMessage,
      isLoading,
      messagesContainer,
      sendMessage,
      quickAction,
      formatTime,
      formatAmount,
      clearHistory
    }
  }
}
</script>

<style scoped>
.ai-chat-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.chat-header {
  text-align: center;
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
}

.chat-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.chat-header p {
  margin: 0;
  opacity: 0.9;
}

.chat-header .header-actions {
  margin-top: 10px;
}

.chat-header .clear-btn {
  padding: 8px 16px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.chat-header .clear-btn:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 20px;
  max-height: 500px;
}

.message {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.message.user {
  align-items: flex-end;
}

.message.ai {
  align-items: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.message.user .message-content {
  background: #007bff;
  color: white;
}

.message.ai .message-content {
  background: white;
  color: #333;
  border: 1px solid #e9ecef;
}

.message-text {
  white-space: pre-wrap;
  line-height: 1.5;
}

.message-data {
  margin-top: 10px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 12px;
  overflow-x: auto;
}

/* 单个账单样式 */
.single-bill {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.created-bill {
  background: linear-gradient(135deg, #e8f5e8 0%, #f0f9ff 100%);
  border-left: 4px solid #67c23a;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 账单列表样式 */
.bill-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bill-item {
  background: white;
  border-radius: 8px;
  padding: 12px;
  border-left: 4px solid #007bff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.bill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.bill-id {
  font-weight: bold;
  color: #007bff;
  font-size: 14px;
}

.bill-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.bill-type.income {
  background: #e8f5e8;
  color: #67c23a;
}

.bill-type.expense {
  background: #ffe8e8;
  color: #f56c6c;
}

.bill-amount {
  font-weight: bold;
  font-size: 16px;
}

.bill-amount.income {
  color: #67c23a;
}

.bill-amount.expense {
  color: #f56c6c;
}

.bill-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
  color: #666;
}

.bill-category {
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.bill-time {
  color: #999;
  font-size: 12px;
}

.bill-remarks {
  font-size: 12px;
  color: #666;
  font-style: italic;
  padding: 4px 0;
  border-top: 1px solid #eee;
  margin-top: 6px;
}

/* 单个分类样式 */
.single-category {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.category-name {
  font-weight: 500;
  color: #333;
}

.category-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.category-type.income {
  background: #e8f5e8;
  color: #67c23a;
}

.category-type.expense {
  background: #ffe8e8;
  color: #f56c6c;
}

/* 分类列表样式 */
.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.category-name {
  font-weight: 500;
  color: #333;
}

.category-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.category-type.income {
  background: #e8f5e8;
  color: #67c23a;
}

.category-type.expense {
  background: #ffe8e8;
  color: #f56c6c;
}

/* 原始数据样式 */
.raw-data {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 8px;
}

.raw-data pre {
  margin: 0;
  font-size: 11px;
  color: #666;
}

.message-time {
  font-size: 12px;
  color: #6c757d;
  margin-top: 5px;
}

.chat-input-container {
  margin-bottom: 20px;
}

.input-wrapper {
  display: flex;
  gap: 10px;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 25px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.chat-input:focus {
  border-color: #007bff;
}

.chat-input:disabled {
  background: #f8f9fa;
  cursor: not-allowed;
}

.send-button {
  padding: 12px 24px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.send-button:hover:not(:disabled) {
  background: #0056b3;
}

.send-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.quick-actions {
  background: white;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.quick-actions h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

@media (max-width: 768px) {
  .ai-chat-container {
    padding: 10px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-btn {
    text-align: center;
  }
}
</style> 