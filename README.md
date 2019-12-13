# What is this?

A simple script that can be run on a Windows machine running a VM to forward a port from the local machine to the VM. It has only been tested on Windows 10.

# When should I use it?

If you have a VM running on a Windows host and there's a process running on the VM listening on a port and you want to access it on your local Windows machine as if it was running locally, you can use this tool.

# How do I use it?

1. Checkout the repo
2. Navigate to the bin folder
3. Right click 'vm-port-tunnel.bat' and 'Run as administrator' (this is necessary because the program uses the netsh command, which requires admin privileges)