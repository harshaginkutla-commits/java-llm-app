#!/bin/bash
set -e

echo "── Installing kubectl ──"
curl -sLO "https://dl.k8s.io/release/$(curl -sL https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl && sudo mv kubectl /usr/local/bin/

echo "── Installing kind ──"
curl -sLo kind https://kind.sigs.k8s.io/dl/v0.23.0/kind-linux-amd64
chmod +x kind && sudo mv kind /usr/local/bin/

echo "── Versions ──"
kubectl version --client
kind version
