This project is an implementation of simple, no framework monitoring system.

## Requirement Analysis

### TBC (Assumptions)
- Threads are to be used, so no modern concurrency JDK classes are to be used for spawning new processes
- Using unit test framework is fine
- JDK ping is to be used for the ping
- HTTP means all hosts are accessible via HTTP protocol on 80
- Report receiving endpoint is HTTP on 80


### TBA (Decisions)
- There will be no validation of configured data and hosts in the MPV
- The configuration format would be "properties" (not YAML or JSON), not to creep the scope in absence of frameworks
- Since only the last results are to be persisted, persistent key-value storage will be used.
- Pinging and reporting functionalities can be split. They won't be in the MVP for TTM. However, it is recommended to keep the separation in mind while coding.
- No scalability is envisaged in MVP
- No retry strategy for failing servers
- No containerization for the MVP; 
  - Also means storage is expected to be started externally.
- Logs would be simple synchronous write to file. Only warning level is to be supported for the MVP. Log level configuration would be a mock at this stage.

### Architecture

#### Main functionality
- Parse configuration file
- Ping via ICMP 
- Ping via HTTP
- Trace route
- Save results to a persistence storage
- Schedule new Threads for pinging and trace routes
- Report last requests for a host on a failure
  - get last entries from the persistent storage
  - simply serialize to JSON
  - HTTP POST
- Manage threads
- Log ping failures to a local file

#### Configuration
Would consist of the following entries:
- array of hosts to ping
- report receiving host
- ICMP ping interval
- ICMP ping timeout
- HTTP ping interval
- HTTP ping timeout
- Trace route command line
- path to log file
- log level 