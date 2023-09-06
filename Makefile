# Variables
SOURCE_DIR = src
BUILD_DIR = bin
MAIN_CLASS = Server
CLASSPATH = $(BUILD_DIR)
threads = 10

# Comandos
JAVAC = javac
JAVA = java

# Reglas
all: clean build run

build:
	@mkdir -p $(BUILD_DIR)
	$(JAVAC) -d $(BUILD_DIR) -cp $(CLASSPATH) $(SOURCE_DIR)/*.java

run:
	@echo "> NOTA: El proceso quedará corriendo en segundo plano con el puerto ocupado\n\r>       Para cerrarlo manualmente copiar las siguientes lineas:"
	@echo "ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs kill"
	@echo "ps -ef | grep DDoS | grep -v grep | awk '{print $2}' | xargs kill \n\r"
	$(JAVA) -cp $(CLASSPATH) $(MAIN_CLASS) $(threads) &
	@sh ./DDoS.sh & 

clean:
	@sh ./killProcess.sh
	@rm -f *.log*
	@rm -rf $(BUILD_DIR)










