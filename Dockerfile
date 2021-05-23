FROM timbru31/java-node:8-jdk-14 as build-stage
WORKDIR /app
COPY . /app
RUN npm install
RUN npm install -g shadow-cljs
RUN shadow-cljs release app

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/resources/public /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]