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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.3.0-ALPHA01/KvsStorage-1.3.0-ALPHA01.xcframework.zip",
            checksum: "912672ed5fa8b3e4107adc3370110463745ecd2628145e69b1f19ab4b22b6a4d"
        )
    ]
)
