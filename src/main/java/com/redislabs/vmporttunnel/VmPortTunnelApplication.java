package com.redislabs.vmporttunnel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class VmPortTunnelApplication implements CommandLineRunner {

	public static final Logger log = LoggerFactory.getLogger(VmPortTunnelApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(VmPortTunnelApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException, InterruptedException {
		if (args.length < 1 || args[0].equals("your-mac-here")) {
			throw new RuntimeException("No Physical Address provided.");
		}

		String physicalAddress = args[0].toLowerCase().replaceAll(":", "-");
		log.info("Physical Address is: {}", physicalAddress);

		SystemCommand.SystemCommandResult res = SystemCommand.run("arp -a");

		String ipLine = res.getOutput().stream()
				.map(String::toLowerCase)
				.filter(l -> l.contains(physicalAddress))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Physical Address not found. Exiting."));

		String ip = ipLine.trim().split(" ")[0];
		log.info("VM IP is: {}", ip);

		res = SystemCommand.run(
				"netsh interface portproxy delete v4tov4 listenaddress=127.0.0.1 listenport=4200"
		);

		if (res.getReturnCode() != 0) {
			log.warn("No existing proxy found.");
		} else {
			log.info("Existing proxy deleted.");
		}

		res = SystemCommand.run(
				"netsh interface portproxy add v4tov4 " +
						"listenaddress=127.0.0.1 listenport=4200 " +
						"connectaddress=" + ip + " connectport=4200"
		);

		if (res.getReturnCode() != 0) {
			throw new RuntimeException("Proxy creation failed.");
		}

		log.info("Proxy created.");
	}
}
