// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "KvsStorage",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(
            name: "KvsStorage",
            targets: ["KvsStorage"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "KvsStorage",
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/v1.1.0/KvsStorage-1.1.0.xcframework.zip",
            checksum: "f0c6a68daa1031df0d0ba2509173e81a00407773fd87b301ab33a4af4c4b57fc"
        )
    ]
)
