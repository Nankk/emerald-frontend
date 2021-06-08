#!/bin/sh
if [ ! -z "$SERVER_URL" ]; then
  echo "[INFO] Generating config.js..."
  envsubst < /usr/share/nginx/html/js/config.js.template > /usr/share/nginx/html/js/config.js
fi
nginx -g "daemon off;"