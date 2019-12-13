package com.redislabs.vmporttunnel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class VmPortTunnelApplication implements CommandLineRunner {

	public static final Logger log = LoggerFactory.getLogger(VmPortTunnelApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(VmPortTunnelApplication.class, args);
	}

	private final String vmMac;
	private final List<String> ports;

	public VmPortTunnelApplication(
			@Value("${mac}") String vmMac,
			@Value("#{'${ports}'.split(',')}") List<String> ports
	) {
		this.vmMac = vmMac;
		this.ports = ports;
	}

	@Override
	public void run(String... args) throws IOException, InterruptedException {
		String physicalAddress = this.vmMac.toLowerCase().replaceAll(":", "-");
		log.info("Physical Address is: {}", physicalAddress);

		SystemCommand.SystemCommandResult res = SystemCommand.run("arp -a");

		String ipLine = res.getOutput().stream()
				.map(String::toLowerCase)
				.filter(l -> l.contains(physicalAddress))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Physical Address not found. Exiting."));

		String ip = ipLine.trim().split(" ")[0];
		log.info("VM IP is: {}", ip);

		mapAllPorts(ip);
	}

	private void mapAllPorts(String ip) throws IOException, InterruptedException {
		for (String port : this.ports) {
			String[] parts = port.split(":");

			if (parts.length != 2) {
				throw new RuntimeException("Can't interpret '" + port + "'");
			}

			int hostPort = Integer.parseInt(parts[0]);
			int vmPort = Integer.parseInt(parts[1]);

			mapPort(hostPort, ip, vmPort);
		}
	}

	private void mapPort(int hostPort, String vmIp, int vmPort) throws IOException, InterruptedException {
		SystemCommand.SystemCommandResult res = SystemCommand.run(
				"netsh interface portproxy delete v4tov4 listenaddress=127.0.0.1 listenport=" + hostPort
		);

		if (res.getReturnCode() != 0) {
			log.warn("No existing proxy found on port {}", hostPort);
		} else {
			log.info("Existing proxy for port {} deleted", hostPort);
		}

		res = SystemCommand.run(
				"netsh interface portproxy add v4tov4 " +
						"listenaddress=127.0.0.1 listenport=" + hostPort + " " +
						"connectaddress=" + vmIp + " connectport=" + vmPort
		);

		if (res.getReturnCode() != 0) {
			throw new RuntimeException("Proxy creation failed.");
		}

		log.info("Tunnel localhost:{} -> {}:{} created", hostPort, vmIp, vmPort);
	}
}
