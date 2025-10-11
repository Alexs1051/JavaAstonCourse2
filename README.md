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

    // Конструкторы
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String name, String email, Integer age) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
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
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);
            transaction.commit();

            logger.info("User saved successfully with ID: {}", user.getId());
            return user;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error saving user with email: {}", user.getEmail(), e);
            throw new UserServiceException("Failed to save user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws UserServiceException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);

            if (user != null) {
                logger.debug("User found with ID: {}", id);
            } else {
                logger.debug("User not found with ID: {}", id);
            }

            return Optional.ofNullable(user);

        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw new UserServiceException("Failed to find user by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws UserServiceException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            List<User> users = query.list();

            logger.debug("Found {} users", users.size());
            return users;

        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new UserServiceException("Failed to find all users: " + e.getMessage(), e);
        }
    }

    @Override
    public User update(User user) throws UserServiceException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User updatedUser = session.merge(user);
            transaction.commit();

            logger.info("User updated successfully with ID: {}", updatedUser.getId());
            return updatedUser;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error updating user with ID: {}", user.getId(), e);
            throw new UserServiceException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) throws UserServiceException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("User deleted successfully with ID: {}", id);
            } else {
                logger.warn("Attempted to delete non-existent user with ID: {}", id);
                throw new UserServiceException("User not found with ID: " + id);
            }

            transaction.commit();

        } catch (UserServiceException e) {
            throw e;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error deleting user with ID: {}", id, e);
            throw new UserServiceException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(Long id) throws UserServiceException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return user != null;
        } catch (Exception e) {
            logger.error("Error checking existence of user with ID: {}", id, e);
            throw new UserServiceException("Failed to check user existence: " + e.getMessage(), e);
        }
    }
}
```
#### Инициализатор базы данных:
```java
public class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/userdb";
    private static final String DB_NAME = "userdb";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "password";

    public static void initialize() {
        logger.info("Starting database initialization...");

        try {
            // Пытаемся подключиться к существующей базе данных
            if (!testDatabaseExists()) {
                createDatabase();
            }

            // Проверяем подключение к целевой базе данных
            testConnection();
            logger.info("Database initialization completed successfully");

        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static boolean testDatabaseExists() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            logger.info("Database '{}' already exists", DB_NAME);
            return true;
        } catch (Exception e) {
            logger.info("Database '{}' does not exist, will create it", DB_NAME);
            return false;
        }
    }

    private static void createDatabase() {
        String createDbSQL = "CREATE DATABASE " + DB_NAME;

        try (Connection conn = DriverManager.getConnection(DEFAULT_URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement()) {

            logger.info("Creating database: {}", DB_NAME);
            stmt.executeUpdate(createDbSQL);
            logger.info("Database '{}' created successfully", DB_NAME);

            // Небольшая задержка для обеспечения создания БД
            Thread.sleep(1000);

        } catch (Exception e) {
            logger.error("Failed to create database '{}'", DB_NAME, e);
            throw new RuntimeException("Database creation failed", e);
        }
    }

    private static void testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            logger.info("Successfully connected to database '{}'", DB_NAME);
        } catch (Exception e) {
            logger.error("Failed to connect to database '{}'", DB_NAME, e);
            throw new RuntimeException("Database connection failed", e);
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
            // Инициализация базы данных
            DatabaseInitializer.initialize();

            // Создание ServiceRegistry
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml")
                    .build();

            // Создание Metadata
            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(com.example.entity.User.class)
                    .getMetadataBuilder()
                    .build();

            // Создание SessionFactory
            sessionFactory = metadata.getSessionFactoryBuilder().build();
            logger.info("Hibernate SessionFactory created successfully");

        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            throw new IllegalStateException("SessionFactory is not available");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        logger.info("Shutting down Hibernate SessionFactory");
        if (sessionFactory != null && !sessionFactory.isClosed()) {
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
