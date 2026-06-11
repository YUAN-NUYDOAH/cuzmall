#!/usr/bin/env bash
# 在 Linux 服务器上为 cuzmall.cn 申请 Let's Encrypt 证书并启用 HTTPS Nginx 配置
set -euo pipefail

DOMAIN="${DOMAIN:-cuzmall.cn}"
EMAIL="${CERTBOT_EMAIL:-}"

if [[ -z "${EMAIL}" ]]; then
  echo "请设置证书通知邮箱: export CERTBOT_EMAIL=your@email.com"
  exit 1
fi

sudo apt-get update
sudo apt-get install -y nginx certbot python3-certbot-nginx

sudo mkdir -p /var/www/certbot
sudo cp deploy/nginx-cuzmall-http-only.conf "/etc/nginx/sites-available/${DOMAIN}"
sudo ln -sf "/etc/nginx/sites-available/${DOMAIN}" "/etc/nginx/sites-enabled/${DOMAIN}"
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx

sudo certbot certonly --webroot \
  -w /var/www/certbot \
  -d "${DOMAIN}" \
  -d "www.${DOMAIN}" \
  --email "${EMAIL}" \
  --agree-tos \
  --no-eff-email

sudo cp deploy/nginx-cuzmall.conf "/etc/nginx/sites-available/${DOMAIN}"
sudo nginx -t
sudo systemctl reload nginx

echo "HTTPS 已启用。证书自动续期: sudo certbot renew --dry-run"
