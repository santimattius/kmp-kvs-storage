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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.1.0-ALPHA01/KvsStorage-1.1.0-ALPHA01.xcframework.zip",
            checksum: "4a114bfa9d2f2f2c6c0fbe1d346109bd9a78e9d97a31e70cb67f41b38f4260c1"
        )
    ]
)
