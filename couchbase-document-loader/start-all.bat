
docker build -t generation-tool .\tools
docker run -v %cd%\java\reactive-data-service:/app/generated generation-tool
echo "data generated"

docker-compose build
echo "build funished"

docker-compose up -d
echo "starting up..."
