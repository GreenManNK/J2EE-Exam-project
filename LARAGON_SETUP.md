# Hướng dẫn cài đặt Laragon 2.1.2 cho ứng dụng HuTech

## 1. Khởi động Laragon và MySQL

### Option A: Sử dụng Laragon GUI (Recommended)
1. Mở `C:\laragon\laragon.exe`
2. Click nút **"Start All"** để khởi động tất cả dịch vụ (Apache, MySQL, PHP)
3. Chờ tới khi trạng thái chuyển thành **"Running"**

### Option B: Dùng Command Line
```powershell
# Khởi động MySQL (MariaDB 10.1.9) từ Laragon
C:\laragon\bin\mysql\mariadb-10.1.9\bin\mysqld.exe --defaults-file=C:\laragon\bin\mysql\mariadb-10.1.9\my.ini

# HOẶC khởi động MySQL 8.4.3 (nếu cấu hình cho phép)
C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysqld.exe
```

## 2. Tạo cơ sở dữ liệu

Sau khi MySQL chạy, tạo database `hutech`:

```powershell
# Sử dụng mysql CLI
C:\laragon\bin\mysql\mariadb-10.1.9\bin\mysql.exe -u root -e "CREATE DATABASE IF NOT EXISTS hutech CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Hoặc truy cập **Laragon GUI > Database > Create Database > hutech**

## 3. Xác thực kết nối

```powershell
# Kiểm tra xem MySQL đang lắng nghe trên port 3306
netstat -aon | findstr :3306

# Hoặc test kết nối trực tiếp
C:\laragon\bin\mysql\mariadb-10.1.9\bin\mysql.exe -u root -h localhost -e "SELECT VERSION();"
```

## 4. Khởi động ứng dụng Spring Boot

```powershell
cd C:\Users\GreenManNK\OneDrive\Documents\hutech
./mvnw clean compile spring-boot:run
```

Ứng dụng sẽ:
- Tự động tạo các bảng cần thiết (ddl-auto=update)
- Kết nối tới database `hutech` trên localhost:3306 với tài khoản `root` (không có mật khẩu)
- Chạy trên port `8083`

## 5. Truy cập ứng dụng

- **Web**: http://localhost:8083
- **phpmyadmin** (nếu Laragon có): http://localhost/phpmyadmin

## Cấu hình connection string

File: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hutech?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## Ghi chú quan trọng

⚠️ **Nếu gặp lỗi "Can't connect to MySQL server"**:
- Kiểm tra Laragon đã được khởi động? (`laragon.exe -> Start All`)
- Kiểm tra MySQL đang lắng nghe trên port 3306? (`netstat -aon | findstr :3306`)
- Database `hutech` đã được tạo? 
- Tài khoản `root` có mục đích truy cập localhost?

⚠️ **Nếu Laragon không khởi động được MySQL**:
- Kiểm tra phiên bản MySQL/MariaDB trong `C:\laragon\bin\mysql\`
- Có thể Laragon sử dụng MariaDB 10.1.9 hoặc MySQL 8.4.3
- Nếu cần, hãy khởi động MariaDB thay vì MySQL

## Database Schema (tự động tạo từ entities)

- `users` - Người dùng
- `categories` - Danh mục sản phẩm
- `products` - Sản phẩm
- `reviews` - Đánh giá
- `carts` - Giỏ hàng
- `cart_items` - Chi tiết giỏ hàng
- `orders` - Đơn hàng
- `order_details` - Chi tiết đơn hàng
- `wishlists` - Danh sách yêu thích
