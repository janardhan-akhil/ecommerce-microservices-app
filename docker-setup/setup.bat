@echo off
REM ─────────────────────────────────────────────────────────────────
REM  E-Commerce Docker Setup — Windows
REM  Run this ONCE from the ecommerce/ root folder
REM  (the folder that contains all your service folders)
REM ─────────────────────────────────────────────────────────────────

echo.
echo [1/4] Copying Dockerfile into every service...
copy /Y docker-setup\Dockerfile eureka-server\Dockerfile
copy /Y docker-setup\Dockerfile user-service\Dockerfile
copy /Y docker-setup\Dockerfile product-service\Dockerfile
copy /Y docker-setup\Dockerfile order-service\Dockerfile
copy /Y docker-setup\Dockerfile cart-service\Dockerfile
copy /Y docker-setup\Dockerfile payment-service\Dockerfile
copy /Y docker-setup\Dockerfile api-gateway\Dockerfile
echo    Done.

echo.
echo [2/4] Copying application-docker.yml into each service...
copy /Y docker-setup\docker-profiles\eureka-server\application-docker.yml  eureka-server\src\main\resources\application-docker.yml
copy /Y docker-setup\docker-profiles\user-service\application-docker.yml   user-service\src\main\resources\application-docker.yml
copy /Y docker-setup\docker-profiles\product-service\application-docker.yml product-service\src\main\resources\application-docker.yml
copy /Y docker-setup\docker-profiles\order-service\application-docker.yml  order-service\src\main\resources\application-docker.yml
copy /Y docker-setup\docker-profiles\cart-service\application-docker.yml   cart-service\src\main\resources\application-docker.yml
copy /Y docker-setup\docker-profiles\payment-service\application-docker.yml payment-service\src\main\resources\application-docker.yml
copy /Y docker-setup\docker-profiles\api-gateway\application-docker.yml    api-gateway\src\main\resources\application-docker.yml
echo    Done.

echo.
echo [3/4] Switching Docker Desktop memory to 5 GB...
echo    Open Docker Desktop ^> Settings ^> Resources ^> Memory ^> set to 5120 MB
echo    Press any key once you have done that...
pause >nul

echo.
echo [4/4] Building and starting all containers...
echo    This will take 3-5 minutes on first run (Maven downloads dependencies)
echo.
docker-compose up --build -d

echo.
echo ─────────────────────────────────────────────────────────────────
echo  All done! Services starting up...
echo.
echo  Eureka dashboard : http://localhost:8761
echo  API Gateway      : http://localhost:8080
echo.
echo  Wait ~2 minutes for all services to register in Eureka.
echo  Watch progress with: docker-compose logs -f
echo ─────────────────────────────────────────────────────────────────