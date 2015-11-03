#!/bin/bash
#!/bin/bash
if [ ! -d "bin" ]; then
        mkdir bin
fi

if [ ! -d "bin/META-INF" ]; then
        mkdir bin/META-INF
fi

echo "Compiling..."
javac -s bin/ -d bin/ -sourcepath src/drawingplus/ColorChooser.java src/drawingplus/*.java

cd bin
cat > META-INF/MANIFEST.MF << "EOF"
Manifest-Version: 1.0
Created-By: Spencer Hanson
Main-Class: drawingplus.DrawingPlus
EOF

jar cmf META-INF/MANIFEST.MF DrawingPlus.jar drawingplus/
mv DrawingPlus.jar ../DrawingPlus.jar

