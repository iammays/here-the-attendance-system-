# Step 1: Build the React app using Node.js 20.12.1
FROM node:20.12.1-slim as build

WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install only production dependencies (faster and cleaner)
RUN npm install --production --legacy-peer-deps

# Copy all source files
COPY . .

# Build the React app
RUN npm run build

# Step 2: Serve it with Nginx
FROM nginx:1.25-alpine

# Remove default nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy React build from previous stage
COPY --from=build /app/build /usr/share/nginx/html

# ✅ Copy the custom nginx config
COPY default.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
