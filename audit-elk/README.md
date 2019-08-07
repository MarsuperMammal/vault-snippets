Instructions for use

1. Perform `vagrant up elk` within this directory
2. Perform `vagrant up vault` within this directory
3. Log in to vault box with `vagrant ssh vault`. Create some log events by interacting with vault.
4. Check the log events in kibana at localhost:5601. Log in with username: elastic password: changeme.
5. At first login you will have to configure an index pattern and time filter. Use 'logstash-vault' and 'time'.
