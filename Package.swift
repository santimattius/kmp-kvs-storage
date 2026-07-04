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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/v2.0.0/KvsStorage-2.0.0.xcframework.zip",
            checksum: "cda071ea155270aa5986b04d0c7c80f63ac8927acd6d8c0c3ac1cdb9a1e14a61"
        )
    ]
)
