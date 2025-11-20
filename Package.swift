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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/v1.2.0/KvsStorage-1.2.0.xcframework.zip",
            checksum: "a5c2f05b1586259c48fd88d77f60eb6d2dfa568c26cba6215c9753769764c029"
        )
    ]
)
