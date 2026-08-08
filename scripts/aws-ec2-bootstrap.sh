#!/usr/bin/env bash
set -euo pipefail

sudo dnf update -y
sudo dnf install -y docker git curl unzip java-17-amazon-corretto-devel maven
sudo systemctl enable --now docker
sudo usermod -aG docker "${USER}"

sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

sudo docker --version
sudo docker compose version

echo
echo "Docker is installed. Sign out and reconnect so your group membership takes effect."
