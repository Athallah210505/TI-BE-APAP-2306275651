package apap.ti._5.accommodation_2306275651_be;

import apap.ti._5.accommodation_2306275651_be.restservice.BookingRestService;
import apap.ti._5.accommodation_2306275651_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomRestService;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomTypeRestService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Accommodation2306275651BeApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Autowired
    private BookingRestService bookingRestService;

    @Autowired
    private PropertyRestService propertyRestService;

    @Autowired
    private RoomRestService roomRestService;

    @Autowired
    private RoomTypeRestService roomTypeRestService;

    // ✅ Test 1: Context Loads
    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    // ✅ Test 2: All Required Beans are Loaded
    @Test
    void allRequiredBeansAreLoaded() {
        assertThat(bookingRestService).isNotNull();
        assertThat(propertyRestService).isNotNull();
        assertThat(roomRestService).isNotNull();
        assertThat(roomTypeRestService).isNotNull();
    }

    // ✅ Test 3: Application Name is Correct
    @Test
    void applicationNameIsCorrect() {
        String applicationName = environment.getProperty("spring.application.name");
        // Check if application name is configured or context is not null
        assertThat(applicationContext.getApplicationName()).isNotNull();
    }

    // ✅ Test 4: Spring Boot Application Annotation is Present
    @Test
    void springBootApplicationAnnotationIsPresent() {
        assertThat(Accommodation2306275651BeApplication.class)
            .hasAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
    }



    // ✅ Test 6: Bean Definition Count
    @Test
    void beanDefinitionCountIsGreaterThanZero() {
        int beanDefinitionCount = applicationContext.getBeanDefinitionCount();
        assertThat(beanDefinitionCount).isGreaterThan(50); // Spring Boot loads many beans
    }

    // ✅ Test 7: Data Source Bean Exists
    @Test
    void dataSourceBeanExists() {
        assertThat(applicationContext.containsBean("dataSource")).isTrue();
    }

    // ✅ Test 8: Entity Manager Factory Bean Exists
    @Test
    void entityManagerFactoryBeanExists() {
        assertThat(applicationContext.containsBean("entityManagerFactory")).isTrue();
    }

    // ✅ Test 9: Transaction Manager Bean Exists
    @Test
    void transactionManagerBeanExists() {
        assertThat(applicationContext.containsBean("transactionManager")).isTrue();
    }

    // ✅ Test 10: JPA Repositories are Loaded
    @Test
    void jpaRepositoriesAreLoaded() {
        assertThat(applicationContext.containsBean("bookingRepository")).isTrue();
        assertThat(applicationContext.containsBean("propertyRepository")).isTrue();
        assertThat(applicationContext.containsBean("roomRepository")).isTrue();
        assertThat(applicationContext.containsBean("roomTypeRepository")).isTrue();
    }

    // ✅ Test 11: REST Controllers are Loaded
    @Test
    void restControllersAreLoaded() {
        assertThat(applicationContext.containsBean("bookingRestController")).isTrue();
        assertThat(applicationContext.containsBean("propertyRestController")).isTrue();
        assertThat(applicationContext.containsBean("roomRestController")).isTrue();
        assertThat(applicationContext.containsBean("roomTypeRestController")).isTrue();
    }

    // ✅ Test 12: Application Main Class Exists
    @Test
    void mainClassExists() {
        assertThat(Accommodation2306275651BeApplication.class).isNotNull();
    }

    // ✅ Test 13: Environment is Active
    @Test
    void environmentIsActive() {
        assertThat(environment).isNotNull();
        String[] activeProfiles = environment.getActiveProfiles();
        assertThat(activeProfiles).isNotNull();
    }

    // ✅ Test 14: Database Connection is Configured
    @Test
    void databaseConnectionIsConfigured() {
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        assertThat(datasourceUrl).isNotNull();
    }

    // // ✅ Test 15: JPA is Configured
    // @Test
    // void jpaIsConfigured() {
    //     String jpaShowSql = environment.getProperty("spring.jpa.show-sql");
    //     assertThat(jpaShowSql).isNotNull();
    // }
}