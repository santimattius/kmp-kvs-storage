import SwiftUI
import KvsStorage

struct ContentView: View {
    private let kvs = Storage.shared.encryptKvs(name: "user_preferences", key: "secret")
    
    @State private var isDarkModeEnabled = false
    
    var body: some View {
        VStack {
            VStack(spacing: 16) {
                Image(systemName: "swift")
                    .font(.system(size: 80))
                    .foregroundColor(.accentColor)
                Text("Kvs Storage")
            }
            Toggle(isOn: $isDarkModeEnabled) {
                 Text("Is Dark Mode Enabled")
            }.onChange(of: isDarkModeEnabled){
                updateValue(value: isDarkModeEnabled)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
        .background(isDarkModeEnabled ? Color.black: Color.white)
        .environment(\.colorScheme, isDarkModeEnabled ? .dark : .light)
        .task {
            for await isEnabled in kvs.getBooleanAsStream( key: "is_dark_mode_enabled", defValue: false) {
                self.isDarkModeEnabled = isEnabled.boolValue
            }
            /*self.isDarkModeEnabled = try! await kvs.getBoolean(
                key: "is_dark_mode_enabled", defValue: false
            ).boolValue
             */
        }
    }
    
    func updateValue(value:Bool)  {
        Task{
            let result = try await kvs.edit()
                .putBoolean(key: "is_dark_mode_enabled", value: value)
                .putString(key: "test_string", value: "Hello World!")
                .apply()
            switch result {
                case .success:
                print("Successfully saved data.")
                case .failure(let error):
                print("Failed to save data: \(error)")
            }
        }
        
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
