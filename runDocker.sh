#!/bin/bash
docker run -d --network=host  --restart unless-stopped --name db-reconstructor --log-opt max-size=100m --log-opt max-file=10 db-reconstructor:0.1


