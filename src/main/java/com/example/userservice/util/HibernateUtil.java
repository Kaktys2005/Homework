package com.example.userservice.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.util.Properties;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static Configuration configuration;

    public static void initialize() {
        if (sessionFactory == null) {
            try {
                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

                if (configuration != null) {
                    // Применяем настройки из кастомной конфигурации
                    Properties props = new Properties();
                    props.putAll(configuration.getProperties());
                    registryBuilder.applySettings(props);
                } else {
                    registryBuilder.configure("hibernate.cfg.xml");
                }

                StandardServiceRegistry registry = registryBuilder.build();
                MetadataSources sources = new MetadataSources(registry);

                // Если есть кастомная конфигурация, вручную добавляем классы
                if (configuration != null) {
                    // Здесь нужно вручную добавить все entity-классы
                    sources.addAnnotatedClass(com.example.userservice.entity.User.class);
                    // Добавьте другие классы по аналогии
                }

                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();
            } catch (Exception e) {
                System.err.println("SessionFactory creation failed: " + e);
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public static void setConfiguration(Configuration config) {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        configuration = config;
        sessionFactory = null;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            initialize();
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        sessionFactory = null;
        configuration = null;
    }
}