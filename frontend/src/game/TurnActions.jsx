
export default function TurnActions({
    error,
    children,
}) {
    return (
        <>
            {children}
            {error && (
                <div className="gameplay-floating-error" role="alert">
                    {error}
                </div>
            )}
        </>
    );
}
