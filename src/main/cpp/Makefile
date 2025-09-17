# Makefile for GeographicLib JNI wrapper

# 编译器设置
CXX = g++
CXXFLAGS = -std=c++11 -Wall -Wextra -fPIC -D_GNU_SOURCE -I/opt/anaconda3/envs/geographic_env/include -I/opt/anaconda3/envs/geographic_env/include/darwin -I./include -I/opt/homebrew/Cellar/geographiclib/2.5.2/include
LDFLAGS = -shared -L/opt/homebrew/lib -lGeographicLib -Wl,-install_name,libgeographiclib_jni.dylib

# 源文件和目标文件
SRCS = GeographicLibWrapper.cpp
OBJS = $(SRCS:.cpp=.o)
TARGET = libgeographiclib_jni.dylib

# 默认目标
all: $(TARGET)

# 生成共享库
$(TARGET): $(OBJS)
	$(CXX) $(LDFLAGS) -o $@ $^

# 编译源文件
%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

# 清理生成的文件
clean:
	rm -f $(OBJS) $(TARGET)

# 安装库文件到系统库目录（需要管理员权限）
install:
	sudo cp $(TARGET) /usr/local/lib/

.PHONY: all clean install