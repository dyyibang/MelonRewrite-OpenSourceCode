#version 330 core

uniform vec3 minBox;
uniform vec3 maxBox;
uniform vec4 boxColor;
uniform mat4 projection;
uniform mat4 modelView;

in vec3 position;
out vec4 fragColor;

void main() {
    // Define the box dimensions in normalized device coordinates (NDC)

    // Get fragment position in NDC
    vec4 fragPos = projection * modelView * vec4(position, 1.0);

    // Apply fade based on fragment position
    float isInsideBox = step(minBox.x, fragPos.x) * step(fragPos.x, maxBox.x) *
    step(minBox.y, fragPos.y) * step(fragPos.y, maxBox.y) *
    step(minBox.z, fragPos.z) * step(fragPos.z, maxBox.z);

    // Output the final color
    fragColor = boxColor * isInsideBox;
}
