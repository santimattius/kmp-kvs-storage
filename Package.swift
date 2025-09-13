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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/v1.0.0/KvsStorage-1.0.0.xcframework.zip",
            checksum: "3eaf69eece4d873c657a9a5ad66399724e3b0f093a01b2d38d5b6770eb384ae1"
        )
    ]
)
