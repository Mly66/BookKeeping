package cn.nbmly.ai.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "booking_number", unique = true, nullable = false)
	private String bookingNumber;

	@ManyToOne
	@JoinColumn(name = "flight_id", nullable = false)
	private Flight flight;

	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Column(name = "booking_date", nullable = false)
	private LocalDate date;

	@Column(name = "booking_status", nullable = false, columnDefinition = "VARCHAR(20)")
	@Enumerated(EnumType.STRING)
	private BookingStatus bookingStatus;

	@Column(name = "booking_class", nullable = false, columnDefinition = "VARCHAR(20)")
	@Enumerated(EnumType.STRING)
	private BookingClass bookingClass;

	@Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
	private BigDecimal price;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Booking() {
	}

	public Booking(String bookingNumber, Flight flight, Customer customer, LocalDate date,
			BookingStatus bookingStatus, BookingClass bookingClass, BigDecimal price) {
		this.bookingNumber = bookingNumber;
		this.flight = flight;
		this.customer = customer;
		this.date = date;
		this.bookingStatus = bookingStatus;
		this.bookingClass = bookingClass;
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBookingNumber() {
		return bookingNumber;
	}

	public void setBookingNumber(String bookingNumber) {
		this.bookingNumber = bookingNumber;
	}

	public Flight getFlight() {
		return flight;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public BookingStatus getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(BookingStatus bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public BookingClass getBookingClass() {
		return bookingClass;
	}

	public void setBookingClass(BookingClass bookingClass) {
		this.bookingClass = bookingClass;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public String getFrom() {
		return flight != null ? flight.getDepartureCity() : null;
	}

	public String getTo() {
		return flight != null ? flight.getArrivalCity() : null;
	}
}