import SwiftUI
import QuickLook

struct ToolCardView: View {
    let tool: ToolType
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            VStack(alignment: .leading, spacing: AppSpacing.space3) {
                // Icon in circle
                ZStack {
                    Circle()
                        .fill(Color.appPrimaryLight)
                        .frame(width: 44, height: 44)

                    Image(systemName: tool.iconName)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Color.appPrimary)
                }

                Spacer(minLength: 4)

                Text(tool.title)
                    .font(AppTypography.bodyLarge)
                    .bold()
                    .foregroundColor(Color.appTextPrimary)
                    .lineLimit(1)

                Text(tool.description)
                    .font(AppTypography.caption)
                    .foregroundColor(Color.appTextMuted)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .padding(AppSpacing.space4)
            .frame(maxWidth: .infinity, minHeight: 140, alignment: .topLeading)
            .background(Color.appSurface)
            .cornerRadius(AppShapes.cardRadius)
            .overlay(
                RoundedRectangle(cornerRadius: AppShapes.cardRadius)
                    .stroke(Color.appBorder.opacity(0.6), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct RecentFilesView: View {
    let recentFiles: [RecentFile]
    let onOpen: (RecentFile) -> Void
    let onShare: (RecentFile) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: AppSpacing.space2) {
            HStack {
                Image(systemName: "clock.arrow.circlepath")
                    .foregroundColor(Color.appTextMuted)
                    .font(.system(size: 14))
                Text("Recent Files")
                    .font(AppTypography.headline)
                    .foregroundColor(Color.appTextPrimary)
            }
            .padding(.horizontal, AppSpacing.space4)

            VStack(spacing: AppSpacing.space2) {
                ForEach(recentFiles) { file in
                    HStack(spacing: AppSpacing.space3) {
                        ZStack {
                            Circle()
                                .fill(Color.appPrimaryLight)
                                .frame(width: 36, height: 36)
                            Image(systemName: file.toolType.iconName)
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(Color.appPrimary)
                        }

                        VStack(alignment: .leading, spacing: 2) {
                            Text(file.name)
                                .font(AppTypography.body)
                                .bold()
                                .foregroundColor(Color.appTextPrimary)
                                .lineLimit(1)

                            Text("\(file.formattedSize) • \(formattedDate(file.timestamp))")
                                .font(AppTypography.caption)
                                .foregroundColor(Color.appTextMuted)
                        }

                        Spacer()

                        Button(action: { onShare(file) }) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 14))
                                .foregroundColor(Color.appTextMuted)
                                .padding(8)
                        }

                        Button(action: { onOpen(file) }) {
                            Image(systemName: "arrow.up.right.square")
                                .font(.system(size: 14))
                                .foregroundColor(Color.appPrimary)
                                .padding(8)
                        }
                    }
                    .padding(AppSpacing.space3)
                    .background(Color.appSurface)
                    .cornerRadius(AppShapes.buttonRadius)
                    .overlay(
                        RoundedRectangle(cornerRadius: AppShapes.buttonRadius)
                            .stroke(Color.appBorder.opacity(0.5), lineWidth: 1)
                    )
                }
            }
            .padding(.horizontal, AppSpacing.space4)
        }
    }

    private func formattedDate(_ timestamp: Double) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, h:mm a"
        return formatter.string(from: Date(timeIntervalSince1970: timestamp))
    }
}
