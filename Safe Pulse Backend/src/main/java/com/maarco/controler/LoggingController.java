package com.maarco.controler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController

@RequestMapping("/logging")
public class LoggingController {
    Logger logger = LoggerFactory.getLogger(LoggingController.class);

    // to get the logs of different types like error, trace, debug, info, warn
    @GetMapping("/logs")
    public String index() {
        logger.trace("A TRACE Message");
        logger.debug("A DEBUG Message");
        logger.info("An INFO Message");
        logger.warn("A WARN Message");
        logger.error("An ERROR Message");

        return "Howdy! Check out the Logs to see the output...";
    }

    // for downloading the logs
    @RequestMapping("/downloadLog")
    public ResponseEntity<Resource> downloadLogFile() throws IOException {
        // Specify the path to your log file
        String logFilePath = "./logs/application.log";
        Path logPath = Paths.get(logFilePath);
        // Create a FileSystemResource representing the log file
        Resource resource = new org.springframework.core.io.FileSystemResource(logPath);
        // Prepare response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=application.log");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // Return the file for download
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}

