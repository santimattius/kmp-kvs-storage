import SwiftUI
import KvsStorage

struct EncryptDocumentDemoView: View {
    private let document = Storage.shared.encryptDocument(name: "encrypt_document_demo", secretKey: "demo_secret")

    @State private var inputValue = ""
    @State private var result: String?
    @State private var isLoading = false
    @State private var error: String?

    var body: some View {
        DemoContentView(
            title: "Document (Encrypted)",
            inputValue: $inputValue,
            result: result,
            isLoading: isLoading,
            error: error,
            onPut: put,
            onGet: get
        )
    }

    private func put() {
        Task {
            isLoading = true
            error = nil
            do {
                try await document.write(value: inputValue)
                result = "Saved"
            } catch {
                self.error = "\(error)"
            }
            isLoading = false
        }
    }

    private func get() {
        Task {
            isLoading = true
            error = nil
            do {
                result = try await document.read()
            } catch {
                self.error = "\(error)"
            }
            isLoading = false
        }
    }
}
