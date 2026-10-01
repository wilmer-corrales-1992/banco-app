# Etapa 1: instala dependencias, corre las pruebas de Jest y genera el build de produccion.
FROM node:24-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npm test -- --ci && npm run build

# Etapa 2: Nginx sirve la aplicacion y reenvia /api al backend.
FROM nginx:1.29-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/banco-frontend/browser /usr/share/nginx/html

EXPOSE 80
