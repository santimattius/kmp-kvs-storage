// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "KvsStorage",
    products: [
        .library(name: "KvsStorage", targets: ["KvsStorage"])
    ],
    targets: [
        .binaryTarget(
            name: "KvsStorage",
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.0.0-ALPHA03/KvsStorage.xcframework.zip",
            checksum:"c21bc919df2b7f9dd05a281915565b2c8e558dc8af33485ff8a004d4ce520fe4")
    ]
)

