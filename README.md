# Android App for Wi-Fi User Management

This project is an Android application that performs user management and bandwidth allocation on wireless routers deployed at end-point of Terragraph backhaul networks. 

Buffallo AirStation router flashed with open source DD-WRT firmware was used for this project. 

The application gets user MAC address details as input and build a MAC filter which is flashed onto to the NVRAM of the router via SSH channel.

This application will simplify router management and encourage deployment of Terragraph networks in areas will low-skilled supervisors.



