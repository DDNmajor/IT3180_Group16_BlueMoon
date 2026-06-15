package com.bluemoon.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class InternalPortConfig {

    @Value("${bluemoon.internal.port:8081}")
    private int internalPort;

    @EventListener(ApplicationReadyEvent.class)
    public void addInternalPort(ApplicationReadyEvent event) {
        try {
            // Lấy WebServer từ ApplicationContext qua reflection (tránh import class Boot 4.x đổi tên)
            Object ctx = event.getApplicationContext();
            Method getWebServer = ctx.getClass().getMethod("getWebServer");
            Object webServer = getWebServer.invoke(ctx);

            // Lấy Tomcat instance từ TomcatWebServer
            var tomcatField = webServer.getClass().getDeclaredField("tomcat");
            tomcatField.setAccessible(true);
            org.apache.catalina.startup.Tomcat tomcat =
                    (org.apache.catalina.startup.Tomcat) tomcatField.get(webServer);

            Connector connector = new Connector();
            connector.setPort(internalPort);
            connector.setProperty("bindOnInit", "false");
            tomcat.getService().addConnector(connector);
            connector.start();

            log.info("[Internal] Cổng nội bộ {} đã mở — http://localhost:{}/thu-ho/gen",
                    internalPort, internalPort);
        } catch (Exception e) {
            log.error("[Internal] Không thể mở cổng nội bộ {}: {}", internalPort, e.getMessage());
        }
    }
}
