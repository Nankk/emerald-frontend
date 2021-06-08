FROM timbru31/java-node:8-jdk-14 as build-stage
WORKDIR /app
RUN npm install -g shadow-cljs
# cache npm libraries
COPY package.json package-lock.json /app/
RUN npm ci \
    && npm cache clean --force
# cache ClojureScript libraries
COPY shadow-cljs.edn .
RUN shadow-cljs info
# build
COPY . /app
RUN shadow-cljs release app

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/resources/public /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]