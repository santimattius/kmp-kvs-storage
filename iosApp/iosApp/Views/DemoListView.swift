import SwiftUI

/// Entry point listing all 6 `Storage` factory demos, mirroring the Android
/// NavHost routes (`inMemoryKvs`, `kvsLight`, `kvsLightEncrypt`, `kvsOptimized`,
/// `document`, `encryptDocument`).
struct DemoListView: View {
    var body: some View {
        List {
            NavigationLink("In-Memory Kvs") { InMemoryDemoView() }
            NavigationLink("Kvs Light") { KvsLightDemoView() }
            NavigationLink("Kvs Light (Encrypted)") { KvsLightEncryptDemoView() }
            NavigationLink("Kvs Optimized") { KvsOptimizedDemoView() }
            NavigationLink("Document") { DocumentDemoView() }
            NavigationLink("Document (Encrypted)") { EncryptDocumentDemoView() }
        }
        .navigationTitle("Storage Demos")
    }
}
