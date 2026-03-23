package com.example.stamboot.config.logging;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("config/log/stacktrace")
public class LoggerConfigController {

	@GetMapping
	public ResponseEntity<String> setFilteringFlag(@RequestParam Boolean filter, HttpServletRequest request) {
		MgntStackTraceConverter.setCutTheBS(filter);
		return ResponseEntity.ok("Stacktrace filtering flag is set to :" + MgntStackTraceConverter.isCutTheBS());
	}

	@GetMapping("/filter/status")
	public ResponseEntity<String> getFilteringFlag() {
		return ResponseEntity.ok("Stacktrace filtering flag current value is :" + MgntStackTraceConverter.isCutTheBS());
	}
}
