# Use an official Node.js runtime as a parent image
FROM node:18-alpine

# Create and set working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json first (if available)
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of your application code into the container
COPY . .

# Expose the port your application will run on. (This should match the port your service listens on.)
EXPOSE 669

# Start the webpack dev server (adjust the script if needed)
CMD ["npm", "start"]
