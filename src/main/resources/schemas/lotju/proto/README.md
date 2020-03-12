# lam_realtime.proto

## How to generate classes from proto-file?

1. Download protoc (https://developers.google.com/protocol-buffers/docs/downloads)
2. Place it in this directory
3. Compile the proto-file
4. Replace classes in ***src/main/java/fi/ely/lotju/lam/proto***with new classes

## How to compile

    /protoc.exe --java_out=. lam_realtime.proto

## Linux / Mac

Run

    ./build.sh 
    
 It compiles files to `src/main/java/fi/ely/lotju/lam/proto`