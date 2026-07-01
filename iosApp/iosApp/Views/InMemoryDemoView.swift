import SwiftUI
import KvsStorage

private let demoKey = "demo_value"

struct InMemoryDemoView: View {
    private let kvs = Storage.shared.inMemoryKvs(name: "in_memory_demo")

    @State private var inputValue = ""
    @State private var result: String?
    @State private var isLoading = false
    @State private var error: String?

    var body: some View {
        DemoContentView(
            title: "In-Memory Kvs",
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
            let outcome = try await kvs.edit()
                .putString(key: demoKey, value: inputValue)
                .apply()
            isLoading = false
            switch outcome {
            case .success:
                result = "Saved"
            case .failure(let failure):
                error = "\(failure)"
            }
        }
    }

    private func get() {
        Task {
            isLoading = true
            error = nil
            let outcome = try await kvs.getStringAsResult(key: demoKey, defValue: "")
            isLoading = false
            switch outcome {
            case .success(let value):
                result = value
            case .failure(let failure):
                error = "\(failure)"
            }
        }
    }
}
