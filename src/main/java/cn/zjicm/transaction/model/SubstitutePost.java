package cn.zjicm.transaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "substitute_posts")
public class SubstitutePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 40, message = "课程名称最多 40 个字")
    @Column(nullable = false, length = 40)
    private String courseName;

    @NotBlank(message = "代课时间不能为空")
    @Size(max = 60, message = "代课时间最多 60 个字")
    @Column(nullable = false, length = 60)
    private String classTime;

    @NotBlank(message = "代课地点不能为空")
    @Size(max = 80, message = "代课地点最多 80 个字")
    @Column(nullable = false, length = 80)
    private String location;

    @NotNull(message = "代课价格不能为空")
    @DecimalMin(value = "0.01", message = "代课价格必须大于 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 300, message = "补充说明最多 300 个字")
    @Column(length = 300)
    private String description;

    @NotBlank(message = "发布人不能为空")
    @Size(max = 30, message = "发布人最多 30 个字")
    @Column(nullable = false, length = 30)
    private String publisherName;

    private Long ownerUserId;

    private LocalDateTime createdAt = LocalDateTime.now();

    public SubstitutePost() {
    }

    public SubstitutePost(Long id, String courseName, String classTime, String location, BigDecimal price,
                          String description, String publisherName, LocalDateTime createdAt) {
        this.id = id;
        this.courseName = courseName;
        this.classTime = classTime;
        this.location = location;
        this.price = price;
        this.description = description;
        this.publisherName = publisherName;
        this.createdAt = createdAt;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getClassTime() {
        return classTime;
    }

    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
