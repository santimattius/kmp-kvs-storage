import SwiftUI

/// Shared layout used by every per-backend demo view: a text field for the
/// value, Put/Get actions, and a result/error surface.
struct DemoContentView: View {
    let title: String
    @Binding var inputValue: String
    let result: String?
    let isLoading: Bool
    let error: String?
    let onPut: () -> Void
    let onGet: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("Value", text: $inputValue)
                .textFieldStyle(.roundedBorder)

            HStack {
                Button("Put", action: onPut).disabled(isLoading)
                Button("Get", action: onGet).disabled(isLoading)
            }

            if isLoading {
                ProgressView()
            }
            if let result {
                Text("Result: \(result)")
            }
            if let error {
                Text("Error: \(error)")
                    .foregroundColor(.red)
            }

            Spacer()
        }
        .padding()
        .navigationTitle(title)
    }
}
