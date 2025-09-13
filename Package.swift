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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.0.0-ALPHA07/KvsStorage-1.0.0-ALPHA07.xcframework.zip",
            checksum: "c02b7ede36cdb922e3de81637adcd6f3d3ea5e26a2cf255baf4edaafebcd881f"
        )
    ]
)
