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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.0.0-ALPHA06/KvsStorage-1.0.0-ALPHA06.xcframework.zip",
            checksum: "afd08b68b1b57a16988e2e4c32b42b186914dd7467592da1a98e6fc487a72014"
        )
    ]
)
