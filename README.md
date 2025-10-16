# ASTON course homework №2 (SQL. Hibernate. JPA)

## Содержание
1. [Цель работы](#цель-работы)
2. [Задачи](#задачи)
3. [Реализация](#реализация)
4. [Результат](#результат)

---

### Цель работы
Разработать консольное приложение(user-service) на Java, использующее Hibernate для взаимодействия с PostgreSQL, без использования Spring. Приложение должно поддерживать базовые операции CRUD (Create, Read, Update, Delete) над сущностью User.

Требования:
- Использовать Hibernate в качестве ORM.
- База данных — PostgreSQL.
- Настроить Hibernate без Spring, используя hibernate.cfg.xml или properties-файл.
- Реализовать CRUD-операции для сущности User (создание, чтение, обновление, удаление), которая состоит из полей: id, name, email, age, created_at.
- Использовать консольный интерфейс для взаимодействия с пользователем.
- Использовать Maven для управления зависимостями.
- Настроить логирование.
- Настроить транзакционность для операций с базой данных.
- Использовать DAO-паттерн для отделения логики работы с БД.
- Обработать возможные исключения, связанные с Hibernate и PostgreSQL

### Задачи
- [x] Создание пользователя - добавление нового пользователя в систему
- [x] Просмотр пользователя - поиск пользователя по ID
- [x] Список пользователей - получение всех пользователей
- [x] Обновление пользователя - изменение данных существующего пользователя
- [x] Удаление пользователя - удаление пользователя по ID
- [x] Автоматическое создание БД - база данных и таблицы создаются автоматически при первом запуске
- [x] Транзакционность - все операции выполняются в транзакциях
- [x] Логирование - подробное логирование операций и ошибок
- [x] Обработка исключений - корректная обработка ошибок БД и приложения

### Реализация

#### Сущность User:
```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "age")
    private Integer age;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructs
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String name, String email, Integer age) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAge(Integer age) {
        if (age != null && (age < 0 || age > 150)) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        this.age = age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format(
                "User{id=%d, name='%s', email='%s', age=%d, createdAt=%s}",
                id, name, email, age, createdAt
        );
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
```
#### Реализация DAO:
```java
public class UserDaoImpl implements UserDao {
    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) throws UserServiceException {
        return executeInTransaction(session -> {
            session.persist(user);
            logger.info("User saved successfully: {}", user.getEmail());
            return user;
        });
    }

    @Override
    public Optional<User> findById(Long id) throws UserServiceException {
        return executeInTransaction(session -> {
            User user = session.find(User.class, id);
            return Optional.ofNullable(user);
        });
    }

    @Override
    public List<User> findAll() throws UserServiceException {
        return executeInTransaction(session -> {
            Query<User> query = session.createQuery("FROM User", User.class)
                    .setMaxResults(100)
                    .setFirstResult(0);
            return query.list();
        });
    }

    @Override
    public User update(User user) throws UserServiceException {
        return executeInTransaction(session -> {
            User updatedUser = session.merge(user);
            logger.info("User updated successfully: {}", user.getEmail());
            return updatedUser;
        });
    }

    @Override
    public void delete(Long id) throws UserServiceException {
        executeInTransactionVoid(session -> {
            User user = session.find(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("User deleted successfully: {}", id);
            }
        });
    }

    private <T> T executeInTransaction(Function<Session, T> function) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T result = function.apply(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Transaction failed", e);
            throw new UserServiceException("Database operation failed", e);
        }
    }

    private void executeInTransactionVoid(Consumer<Session> consumer) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            consumer.accept(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Transaction failed", e);
            throw new UserServiceException("Database operation failed", e);
        }
    }
}
```
#### Реализация сервиса:
```java
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserDao userDao;

    // Email regexp
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(String name, String email, Integer age) throws UserServiceException {
        logger.debug("Creating new user: name={}, email={}, age={}", name, email, age);

        // Data validation
        validateUserData(name, email, age);

        try {
            // Check if user with this email is already exist
            User user = new User(name, email, age);
            User savedUser = userDao.save(user);

            logger.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (UserServiceException e) {
            logger.error("Failed to create user with email: {}", email, e);
            throw new UserServiceException("Failed to create user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> getUserById(Long id) throws UserServiceException {
        logger.debug("Retrieving user by ID: {}", id);

        if (id == null || id <= 0) {
            logger.warn("Invalid user ID provided: {}", id);
            throw new UserServiceException("Invalid user ID: " + id);
        }

        try {
            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                logger.debug("User found with ID: {}", id);
            } else {
                logger.debug("User not found with ID: {}", id);
            }
            return user;
        } catch (UserServiceException e) {
            logger.error("Failed to retrieve user with ID: {}", id, e);
            throw new UserServiceException("Failed to retrieve user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAllUsers() throws UserServiceException {
        logger.debug("Retrieving all users");

        try {
            List<User> users = userDao.findAll();
            logger.debug("Retrieved {} users", users.size());
            return users;
        } catch (UserServiceException e) {
            logger.error("Failed to retrieve all users", e);
            throw new UserServiceException("Failed to retrieve users: " + e.getMessage(), e);
        }
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) throws UserServiceException {
        logger.debug("Updating user with ID: {}, name={}, email={}, age={}", id, name, email, age);

        if (id == null || id <= 0) {
            logger.warn("Invalid user ID provided for update: {}", id);
            throw new UserServiceException("Invalid user ID: " + id);
        }

        // Data validation (if present)
        if (name != null && name.trim().isEmpty()) {
            throw new UserServiceException("Name cannot be empty");
        }
        if (email != null && !isValidEmail(email)) {
            throw new UserServiceException("Invalid email format: " + email);
        }
        if (age != null && !isValidAge(age)) {
            throw new UserServiceException("Invalid age: " + age);
        }

        try {
            // Get existing user
            Optional<User> existingUser = getUserById(id);
            if (existingUser.isEmpty()) {
                logger.warn("User not found for update with ID: {}", id);
                throw new UserServiceException("User not found with ID: " + id);
            }

            User user = existingUser.get();

            // Update only present fields
            if (name != null) {
                user.setName(name);
            }
            if (email != null) {
                user.setEmail(email);
            }
            if (age != null) {
                user.setAge(age);
            }

            User updatedUser = userDao.update(user);
            logger.info("User updated successfully with ID: {}", updatedUser.getId());
            return updatedUser;
        } catch (UserServiceException e) {
            logger.error("Failed to update user with ID: {}", id, e);
            throw new UserServiceException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(Long id) throws UserServiceException {
        logger.debug("Deleting user with ID: {}", id);

        if (id == null || id <= 0) {
            logger.warn("Invalid user ID provided for deletion: {}", id);
            throw new UserServiceException("Invalid user ID: " + id);
        }

        try {
            if (!userExists(id)) {
                logger.warn("Attempt to delete non-existent user with ID: {}", id);
                throw new UserServiceException("User not found with ID: " + id);
            }

            userDao.delete(id);
            logger.info("User deleted successfully with ID: {}", id);
        } catch (UserServiceException e) {
            logger.error("Failed to delete user with ID: {}", id, e);
            throw new UserServiceException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean userExists(Long id) throws UserServiceException {
        if (id == null || id <= 0) {
            return false;
        }

        try {
            return userDao.findById(id).isPresent();
        } catch (UserServiceException e) {
            logger.error("Failed to check user existence with ID: {}", id, e);
            throw new UserServiceException("Failed to check user existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public boolean isValidAge(Integer age) {
        return age != null && age >= 0 && age <= 150;
    }

    private void validateUserData(String name, String email, Integer age) throws UserServiceException {
        if (name == null || name.trim().isEmpty()) {
            throw new UserServiceException("Name cannot be empty");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new UserServiceException("Email cannot be empty");
        }

        if (!isValidEmail(email)) {
            throw new UserServiceException("Invalid email format: " + email);
        }

        if (age == null || !isValidAge(age)) {
            throw new UserServiceException("Invalid age: " + age);
        }
    }
}
```
#### Инициализатор базы данных:
```java
public class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = "userdb";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "root";

    public static void initialize() {
        try {
            // Attempt to connect to database
            testConnection();
            logger.info("Database connection test successful");
        } catch (Exception e) {
            logger.warn("Database connection failed, attempting to create database...");
            createDatabase();
        }

        // Initialize Hibernate and create tables
        initializeHibernate();
    }

    private static void testConnection() throws Exception {
        String url = DEFAULT_URL + DB_NAME;
        try (Connection connection = DriverManager.getConnection(url, USERNAME, PASSWORD)) {
            // Check connection
        }
    }

    private static void createDatabase() {
        try (Connection connection = DriverManager.getConnection(DEFAULT_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {

            // Create database
            String createDbSQL = "CREATE DATABASE " + DB_NAME;
            statement.executeUpdate(createDbSQL);
            logger.info("Database '{}' created successfully", DB_NAME);

        } catch (Exception e) {
            logger.error("Failed to create database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void initializeHibernate() {
        try {
            // Get SessionFactory - Hibernate will automatically create tables
            HibernateUtil.getSessionFactory();
            logger.info("Hibernate initialized successfully");
        } catch (Exception e) {
            logger.error("Hibernate initialization failed", e);
            throw new RuntimeException("Hibernate initialization failed", e);
        }
    }
}
```
#### Утилита Hibernate:
```java
public class HibernateUtil {
    private static final Logger logger = LogManager.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    static {
        try {
            // Initialize database before creating SessionFactory
            DatabaseInitializer.initialize();

            // Create SessionFactory
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml")
                    .build();

            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(User.class)
                    .getMetadataBuilder()
                    .build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
            logger.info("Hibernate SessionFactory created successfully");

        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
```
#### Конфигурация логирования:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <File name="FileAppender" fileName="logs/user-service.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </File>
    </Appenders>
    <Loggers>
        <Logger name="org.aston.learning.stage2" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="FileAppender"/>
        </Logger>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="FileAppender"/>
        </Root>
    </Loggers>
</Configuration>
```
#### Конфигурация Hibernate:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>
        <!-- Database connection settings -->
        <property name="hibernate.connection.driver_class">org.postgresql.Driver</property>
        <property name="hibernate.connection.url">jdbc:postgresql://localhost:5432/userdb</property>
        <property name="hibernate.connection.username">postgres</property>
        <property name="hibernate.connection.password">root</property>

        <!-- JDBC connection pool settings -->
        <property name="hibernate.connection.pool_size">10</property>

        <!-- Echo all executed SQL to stdout -->
        <property name="hibernate.show_sql">true</property>
        <property name="hibernate.format_sql">true</property>

        <!-- Create the database schema on startup -->
        <property name="hibernate.hbm2ddl.auto">create-drop</property>

        <!-- Other settings -->
        <property name="hibernate.current_session_context_class">thread</property>
        <property name="hibernate.jdbc.lob.non_contextual_creation">true</property>

        <!-- Mention annotated entity class -->
        <mapping class="org.aston.learning.stage2.entity.User"/>
    </session-factory>
</hibernate-configuration>
```
#### Настройка Maven:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.aston.learning.stage2</groupId>
    <artifactId>JavaAstonCourse3</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Hibernate Core -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>7.2.0.CR1</version>
        </dependency>

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.8</version>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.25.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j2-impl</artifactId>
            <version>2.25.2</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Результат

<img width="1054" height="776" alt="Screenshot_3" src="https://github.com/user-attachments/assets/754fa1d4-9023-4431-8166-bcec2ea99082" />
<img width="1058" height="519" alt="Screenshot_5" src="https://github.com/user-attachments/assets/660c8006-243e-4041-94a1-73480ca91293" />
<img width="1048" height="730" alt="Screenshot_6" src="https://github.com/user-attachments/assets/8d3b6c20-c996-47a1-aefe-5a73242ec5aa" />

[⬆️ К содержанию](#содержание)
