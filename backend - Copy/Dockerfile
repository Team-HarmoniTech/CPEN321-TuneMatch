FROM node:20.6-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install

ENV DATABASE_URL="mysql://root:5KvX9bzs8x8VSAmwcD9S@mysql:3306/TuneMatchDB"

COPY . .

EXPOSE 3000

CMD ["npm", "start"]