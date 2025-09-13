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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.0.0-ALPHA08/KvsStorage-1.0.0-ALPHA08.xcframework.zip",
            checksum: "a4ac8b1f28aad92443d9a6c405eae133fc8f13f5acf40c7c5a556e2447b73934"
        )
    ]
)
