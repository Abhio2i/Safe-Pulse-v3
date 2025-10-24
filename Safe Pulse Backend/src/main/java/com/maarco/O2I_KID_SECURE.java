package com.maarco;


import com.maarco.config.AppConstants;
import com.maarco.entities.Role;
import com.maarco.repository.RoleRepo;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class O2I_KID_SECURE implements CommandLineRunner {


    @Autowired
    private RoleRepo roleRepo;

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        return FirebaseAuth.getInstance(firebaseApp());
    }

    private static final Logger LOG = LoggerFactory.getLogger(O2I_KID_SECURE.class);

    public static void main(String[] args) {
        SpringApplication.run(O2I_KID_SECURE.class, args);
//        application.setRegisterShutdownHook(true);
//        application.run(args);
    }


    @Override
    public void run(String... args) throws Exception {

//        System.out.println(this.passwordEncoder.encode("abc"));
        try {

            Role role = new Role();
            role.setId(AppConstants.ADMIN_USER);
            role.setName("ROLE_ADMIN");

            Role role1 = new Role();
            role1.setId(AppConstants.NORMAL_USER);
            role1.setName("ROLE_NORMAL");

            Role role2 = new Role();
            role2.setId(AppConstants.SUPER_USER);
            role2.setName("SUPER_USER");

            //List<Role> roles = List<role>;
            List<Role> roles = new ArrayList<Role>();
            //List<Role> roles = List.of(role,role1);
            roles.add(role);
            roles.add(role1);
            roles.add(role2);
            List<Role> result = this.roleRepo.saveAll(roles);

            result.forEach(r -> {
                System.out.println(r.getName());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        printLog();
    }




    private static void printLog() {
        LOG.debug("Debug Message");
        LOG.warn("Warn Message");
        LOG.error("Error Message");
        LOG.info("Info Message");
        LOG.trace("Trace Message");
    }


    @Bean
    FirebaseMessaging firebaseMassaging() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("firebase-service-account.json").getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials).build();
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions,"o2i-4c18a");
        return FirebaseMessaging.getInstance(app);
    }



    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        FirebaseApp app;
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            app = firebaseApps.get(0);
        } else {
            InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://nin2byo2i-default-rtdb.firebaseio.com")
                    .build();
            app = FirebaseApp.initializeApp(options, "o2i-4c18a");
        }
        return app;
    }

}
