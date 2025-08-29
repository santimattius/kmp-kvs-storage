// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "KvsStorage",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(name: "KvsStorage", targets: ["KvsStorage"])
    ],
    targets: [
        .binaryTarget(
            name: "KvsStorage",
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.0.0-ALPHA01/KvsStorage.xcframework.zip",
            checksum:"b71a0855b9929df491dbaf4f5533c22fee30ef826d948f687c432f71b4790a6b")
    ]
)

