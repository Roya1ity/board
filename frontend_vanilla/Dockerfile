FROM nginx:1.27-alpine
LABEL authors="User"

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY index.html style.css app.js /usr/share/nginx/html/

EXPOSE 8080

ENTRYPOINT ["nginx", "-g","daemon off;"]